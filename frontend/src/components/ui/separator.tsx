import { cn } from '../../lib/utils'

function Separator({ className, orientation = 'horizontal', ...props }: React.HTMLAttributes<HTMLHRElement> & { orientation?: 'horizontal' | 'vertical' }) {
  return (
    <hr
      className={cn(
        'shrink-0 border-slate-200',
        orientation === 'horizontal' ? 'h-[1px] w-full my-4' : 'h-full w-[1px] mx-4',
        className
      )}
      {...props}
    />
  )
}
export { Separator }
